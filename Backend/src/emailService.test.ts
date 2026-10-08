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
  vi.spyOn(console, "error").mockImplementation(() => {});
  vi.spyOn(console, "warn").mockImplementation(() => {});
});

describe("Resend email delivery", () => {
  it("returns failure and logs a generic message for returned Resend errors", async () => {
    resendMocks.send.mockResolvedValue({
      data: null,
      error: {
        name: "invalid_from_address",
        message: "Sender rejected",
        statusCode: 403,
      },
    });

    const sent = await sendVerificationEmail({
      email: "user@example.com",
      name: "User",
      rawToken: verificationToken,
    });

    expect(sent).toBe(false);
    expect(console.error).toHaveBeenCalledExactlyOnceWith("Failed to send verification email via Resend.");

    const loggedOutput = JSON.stringify(vi.mocked(console.error).mock.calls);
    expect(loggedOutput).not.toContain(resendApiKey);
    expect(loggedOutput).not.toContain(verificationToken);
    expect(loggedOutput).not.toContain("Sender rejected");
    expect(loggedOutput).not.toContain("Authorization");
  });

  it("returns failure if a response has no data or error", async () => {
    resendMocks.send.mockResolvedValue({ data: null, error: null });

    const sent = await sendVerificationEmail({
      email: "user@example.com",
      name: "User",
      rawToken: verificationToken,
    });

    expect(sent).toBe(false);
    expect(console.error).toHaveBeenCalledExactlyOnceWith("Failed to send verification email via Resend.");
  });

  it("returns failure and logs a generic message for thrown exceptions", async () => {
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
    expect(console.error).toHaveBeenCalledExactlyOnceWith("Failed to send verification email via Resend.");

    const loggedOutput = JSON.stringify(vi.mocked(console.error).mock.calls);
    expect(loggedOutput).not.toContain(resendApiKey);
    expect(loggedOutput).not.toContain(verificationToken);
    expect(loggedOutput).not.toContain(error.message);
  });
});
