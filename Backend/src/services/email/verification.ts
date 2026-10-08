import nodemailer from "nodemailer";

import { env } from "../../config/env.js";

function hasSmtpConfig(): boolean {
  return Boolean(env.smtpHost && env.smtpUser && env.smtpPassword && env.emailFrom);
}

function createEmailTransporter() {
  return nodemailer.createTransport({
    host: env.smtpHost,
    port: env.smtpPort,
    secure: env.smtpPort === 465,
    auth: {
      user: env.smtpUser,
      pass: env.smtpPassword,
    },
  });
}

export async function sendVerificationEmail(input: {
  email: string;
  name: string;
  rawToken: string;
}): Promise<boolean> {
  const { email, name, rawToken } = input;

  if (!hasSmtpConfig()) {
    console.warn("SMTP email verification is not configured; verification email was not sent.");
    return false;
  }

  const verificationUrl = `${env.appBaseUrl.replace(/\/$/, "")}/api/auth/verify-email?token=${encodeURIComponent(rawToken)}`;

  try {
    await createEmailTransporter().sendMail({
      from: env.emailFrom,
      to: email,
      subject: "Verify your email address",
      html: `
        <p>Hello ${name},</p>
        <p>Thanks for joining. Please verify your email address by clicking below:</p>
        <p><a href="${verificationUrl}">${verificationUrl}</a></p>
        <p>If you did not create this account, you can ignore this email.</p>
      `,
      text: `Hello ${name},\n\nPlease verify your email address using this link:\n${verificationUrl}\n\nIf you did not create this account, you can ignore this email.`,
    });

    return true;
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown SMTP error";
    console.error("Failed to send verification email:", message);
    return false;
  }
}

export async function sendPasswordResetEmail(input: {
  email: string;
  rawToken: string;
}): Promise<boolean> {
  const { email, rawToken } = input;

  if (!hasSmtpConfig()) {
    console.warn("SMTP password reset is not configured; password reset email was not sent.");
    return false;
  }

  const resetUrl = `${env.appBaseUrl.replace(/\/$/, "")}/api/auth/reset-password?token=${encodeURIComponent(rawToken)}`;

  try {
    await createEmailTransporter().sendMail({
      from: env.emailFrom,
      to: email,
      subject: "Reset your password",
      html: `
        <p>We received a request to reset your password.</p>
        <p><a href="${resetUrl}">Reset your password</a></p>
        <p>If you did not request this, you can ignore this email.</p>
        <p>This link expires in 30 minutes.</p>
      `,
      text: `We received a request to reset your password.\n\nReset your password using this link:\n${resetUrl}\n\nIf you did not request this, you can ignore this email. This link expires in 30 minutes.`,
    });

    return true;
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown SMTP error";
    console.error("Failed to send password reset email:", message);
    return false;
  }
}
