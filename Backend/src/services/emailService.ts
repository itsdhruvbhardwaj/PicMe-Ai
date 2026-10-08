import { Resend } from "resend";

import { env } from "../config/env.js";

type EmailMessage = {
  to: string;
  subject: string;
  html: string;
  text: string;
};

type ResendDiagnostic = {
  failureReason: "resend_error" | "missing_data_id" | "exception";
  errorName?: string;
  errorMessage?: string;
  httpStatus?: number;
  resendCode?: string;
  dataPresent: boolean;
  resendId?: string;
};

function safeDiagnosticValue(value: unknown, sensitiveValues: string[]): string | undefined {
  if (typeof value !== "string") {
    return undefined;
  }

  let safeValue = value;
  for (const sensitiveValue of sensitiveValues) {
    if (sensitiveValue) {
      safeValue = safeValue.split(sensitiveValue).join("[REDACTED]");
    }
  }
  return safeValue
    .replace(/([?&]token=)[^&\s"'<>]+/gi, "$1[REDACTED]")
    .replace(/\b(authorization\s*[:=]\s*)(?:bearer\s+)?[^\s,;]+/gi, "$1[REDACTED]")
    .replace(/\bbearer\s+[A-Za-z0-9._~+/-]+=*/gi, "Bearer [REDACTED]")
    .replace(
      /\b((?:access[_-]?token|refresh[_-]?token|password|passwd|secret|api[_-]?key)\s*[=:]\s*)[^,\s&;]+/gi,
      "$1[REDACTED]",
    )
    .replace(/\beyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\b/g, "[REDACTED]")
    .replace(/[\r\n\t]/g, " ")
    .slice(0, 500);
}

function getProperty(value: unknown, key: string): unknown {
  if (value && typeof value === "object" && key in value) {
    return (value as Record<string, unknown>)[key];
  }
  return undefined;
}

function getHttpStatus(error: unknown): number | undefined {
  const status =
    getProperty(error, "statusCode") ??
    getProperty(error, "status") ??
    getProperty(getProperty(error, "response"), "status");
  return typeof status === "number" ? status : undefined;
}

function createResendDiagnostic(
  error: unknown,
  data: { id?: unknown } | null | undefined,
  sensitiveValues: string[],
  failureReason: ResendDiagnostic["failureReason"],
): ResendDiagnostic {
  const errorName = safeDiagnosticValue(
    getProperty(error, "name") ??
      (error instanceof Error ? error.constructor.name : undefined),
    sensitiveValues,
  );
  const errorMessage = safeDiagnosticValue(getProperty(error, "message"), sensitiveValues);
  const resendCode = safeDiagnosticValue(
    getProperty(error, "code") ?? getProperty(error, "name"),
    sensitiveValues,
  );
  const resendId = safeDiagnosticValue(data?.id, sensitiveValues);
  const httpStatus = getHttpStatus(error);

  return {
    failureReason,
    ...(errorName ? { errorName } : {}),
    ...(errorMessage ? { errorMessage } : {}),
    ...(httpStatus !== undefined ? { httpStatus } : {}),
    ...(resendCode ? { resendCode } : {}),
    dataPresent: data !== null && data !== undefined,
    ...(resendId ? { resendId } : {}),
  };
}

function logEmailConfiguration(): void {
  const sender = safeDiagnosticValue(env.emailFrom, [env.resendApiKey]) ?? "";
  console.info(
    `Resend email configuration: RESEND_API_KEY configured: ${Boolean(env.resendApiKey)}; ` +
      `EMAIL_FROM configured: ${Boolean(sender)}; sender: ${sender || "(unset)"}`,
  );
}

async function sendEmail(
  message: EmailMessage,
  purpose: string,
  sensitiveValues: string[],
): Promise<boolean> {
  logEmailConfiguration();

  if (!env.resendApiKey || !env.emailFrom) {
    console.warn(`Resend email ${purpose} is not configured; email was not sent.`);
    return false;
  }

  const diagnosticsSensitiveValues = [
    env.resendApiKey,
    ...sensitiveValues,
    ...sensitiveValues.map((value) => encodeURIComponent(value)),
  ];

  try {
    const resend = new Resend(env.resendApiKey);
    const { data, error } = await resend.emails.send({
      from: env.emailFrom,
      ...message,
    });

    if (error || !data?.id) {
      console.error(
        `Failed to send ${purpose} email via Resend.`,
        createResendDiagnostic(
          error,
          data,
          diagnosticsSensitiveValues,
          error ? "resend_error" : "missing_data_id",
        ),
      );
      return false;
    }

    console.info(`Resend accepted ${purpose} email.`, {
      resendId: safeDiagnosticValue(data.id, diagnosticsSensitiveValues),
    });
    return true;
  } catch (error) {
    console.error(
      `Failed to send ${purpose} email via Resend.`,
      createResendDiagnostic(error, null, diagnosticsSensitiveValues, "exception"),
    );
    return false;
  }
}

export async function sendVerificationEmail(input: {
  email: string;
  name: string;
  rawToken: string;
}): Promise<boolean> {
  const { email, name, rawToken } = input;
  const verificationUrl = `${env.appBaseUrl.replace(/\/$/, "")}/api/auth/verify-email?token=${encodeURIComponent(rawToken)}`;

  return sendEmail(
    {
      to: email,
      subject: "Verify your email address",
      html: `
        <p>Hello ${name},</p>
        <p>Thanks for joining. Please verify your email address by clicking below:</p>
        <p><a href="${verificationUrl}">${verificationUrl}</a></p>
        <p>If you did not create this account, you can ignore this email.</p>
      `,
      text: `Hello ${name},\n\nPlease verify your email address using this link:\n${verificationUrl}\n\nIf you did not create this account, you can ignore this email.`,
    },
    "verification",
    [rawToken],
  );
}

export async function sendPasswordResetEmail(input: {
  email: string;
  rawToken: string;
}): Promise<boolean> {
  const { email, rawToken } = input;
  const resetUrl = `${env.appBaseUrl.replace(/\/$/, "")}/api/auth/reset-password?token=${encodeURIComponent(rawToken)}`;

  return sendEmail(
    {
      to: email,
      subject: "Reset your password",
      html: `
        <p>We received a request to reset your password.</p>
        <p><a href="${resetUrl}">Reset your password</a></p>
        <p>If you did not request this, you can ignore this email.</p>
        <p>This link expires in 30 minutes.</p>
      `,
      text: `We received a request to reset your password.\n\nReset your password using this link:\n${resetUrl}\n\nIf you did not request this, you can ignore this email. This link expires in 30 minutes.`,
    },
    "password reset",
    [rawToken],
  );
}
