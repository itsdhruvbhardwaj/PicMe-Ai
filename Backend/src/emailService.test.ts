import { beforeEach, describe, expect, it, vi } from "vitest";

const resendMocks = vi.hoisted(() => ({
  send: vi.fn(),
}));

vi.mock("resend", () => ({
  Resend: vi.fn().mockImplementation(() => ({
    emails: { send: resendMocks.send },
  })),
}));

import { sendVerificationEmail } from "./services/emailService.js";

const resendApiKey = "test-resend-api-key";
const verificationToken = "secret-verification-token";

beforeEach(() => {
  vi.clearAllMocks();
  process.env.RESEND_API_KEY = resendApiKey;
  process.env.EMAIL_FROM = "onboarding@resend.dev";
  process.env.APP_BASE_URL = "https://picme.example";
  vi.spyOn(console, "info").mockImplementation(() => {});
  vi.spyOn(console, "error").mockImplementation(() => {});
  vi.spyOn(console, "warn").mockImplementation(() => {});
});

describe("Resend email diagnostics", () => {
  it("logs returned Resend error fields without exposing credentials or tokens", async () => {
    resendMocks.send.mockResolvedValue({
      data: null,
      error: {
        name: "invalid_from_address",
        message:
          `Sender rejected for token ${verificationToken} using ${resendApiKey}; ` +
          "Authorization: Bearer access-secret access_token=access-secret " +
          "refresh_token=refresh-secret password=password-secret",
        statusCode: 403,
      },
    });

    const sent = await sendVerificationEmail({
      email: "user@example.com",
      name: "User",
      rawToken: verificationToken,
    });

    expect(sent).toBe(false);
    expect(console.info).toHaveBeenCalledWith(
      "Resend email configuration: RESEND_API_KEY configured: true; EMAIL_FROM configured: true; sender: onboarding@resend.dev",
    );
    expect(console.error).toHaveBeenCalledWith(
      "Failed to send verification email via Resend.",
      expect.objectContaining({
        errorName: "invalid_from_address",
        errorMessage: expect.stringContaining("[REDACTED]"),
        httpStatus: 403,
        resendCode: "invalid_from_address",
        dataPresent: false,
      }),
    );

    const loggedOutput = JSON.stringify(vi.mocked(console.error).mock.calls);
    expect(loggedOutput).not.toContain(resendApiKey);
    expect(loggedOutput).not.toContain(verificationToken);
    expect(loggedOutput).not.toContain("access-secret");
    expect(loggedOutput).not.toContain("refresh-secret");
    expect(loggedOutput).not.toContain("password-secret");
  });

  it("reports a missing data response as a failure", async () => {
    resendMocks.send.mockResolvedValue({ data: null, error: null });

    const sent = await sendVerificationEmail({
      email: "user@example.com",
      name: "User",
      rawToken: verificationToken,
    });

    expect(sent).toBe(false);
    expect(console.error).toHaveBeenCalledWith(
      "Failed to send verification email via Resend.",
      expect.objectContaining({ dataPresent: false }),
    );
  });

  it("logs safe fields from thrown exceptions", async () => {
    const error = Object.assign(
      new Error(`Request failed for ${verificationToken} with ${resendApiKey}`),
      { statusCode: 502, code: "upstream_error" },
    );
    resendMocks.send.mockRejectedValue(error);

    const sent = await sendVerificationEmail({
      email: "user@example.com",
      name: "User",
      rawToken: verificationToken,
    });

    expect(sent).toBe(false);
    expect(console.error).toHaveBeenCalledWith(
      "Failed to send verification email via Resend.",
      expect.objectContaining({
        errorName: "Error",
        httpStatus: 502,
        resendCode: "upstream_error",
        dataPresent: false,
      }),
    );

    const loggedOutput = JSON.stringify(vi.mocked(console.error).mock.calls);
    expect(loggedOutput).not.toContain(resendApiKey);
    expect(loggedOutput).not.toContain(verificationToken);
  });
});
