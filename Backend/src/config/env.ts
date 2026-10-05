import "dotenv/config";

function positiveInteger(name: string, fallback: number): number {
  const value = process.env[name];
  if (!value) return fallback;

  const parsed = Number.parseInt(value, 10);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}

const outputFormats = ["jpeg", "png", "webp"] as const;

export const env = {
  get openAiApiKey() {
    return process.env.OPENAI_API_KEY ?? "";
  },
  get port() {
    return positiveInteger("PORT", 5000);
  },
  get clientOrigins() {
    return (process.env.CLIENT_ORIGIN ?? "")
      .split(",")
      .map((origin) => origin.trim())
      .filter(Boolean);
  },
  get imageModel() {
    return process.env.OPENAI_IMAGE_MODEL ?? "gpt-image-2.5-flare";
  },
  get imageSize() {
    return process.env.OPENAI_IMAGE_SIZE ?? "1024x1536";
  },
  get imageQuality() {
    return process.env.OPENAI_IMAGE_QUALITY ?? "medium";
  },
  get imageFormat() {
    const configuredFormat = process.env.OPENAI_IMAGE_FORMAT ?? "jpeg";
    return outputFormats.includes(configuredFormat as (typeof outputFormats)[number])
      ? (configuredFormat as (typeof outputFormats)[number])
      : "jpeg";
  },
  get openAiTimeoutMs() {
    return positiveInteger("OPENAI_TIMEOUT_MS", 120_000);
  },
  maxUploadBytes: 10 * 1024 * 1024,
};