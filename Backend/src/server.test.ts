import { describe, expect, it, vi } from "vitest";
import request from "supertest";

process.env.OPENAI_API_KEY = "test-api-key";

import { app } from "./server.js";
import { OpenAIImageGenerator } from "./services/openai/generation.js";

vi.mock("openai", () => {
  return {
    default: vi.fn().mockImplementation(() => ({
      images: {
        edit: vi.fn().mockResolvedValue({
          data: [{ b64_json: "abc123" }],
          usage: { input_tokens: 12, output_tokens: 8, total_tokens: 20 },
        }),
        generate: vi.fn(),
      },
    })),
  };
});

describe("backend health", () => {
  it("reports application health", async () => {
    const response = await request(app).get("/health");

    expect(response.status).toBe(200);
    expect(response.body).toMatchObject({ ok: true });
  });
});

describe("styles endpoint", () => {
  it("returns available styles", async () => {
    const response = await request(app).get("/api/styles");

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body.styles)).toBe(true);
    expect(response.body.styles.length).toBeGreaterThan(0);
    for (const style of response.body.styles) {
      expect(style).toHaveProperty("id");
      expect(style).toHaveProperty("name");
      expect(style).toHaveProperty("description");
      expect(style).toHaveProperty("prompt");
      expect(style).toHaveProperty("thumbnail");
      expect([1, 2]).toContain(style.imageCount);
    }
  });
});

describe("generation endpoint", () => {
  it("continues to accept styleId and images", async () => {
    const response = await request(app)
      .post("/api/generate")
      .field("styleId", "professional-portrait")
      .attach("images", Buffer.from("fake-image-data"), {
        filename: "portrait.png",
        contentType: "image/png",
      });

    expect(response.status).toBe(200);
    expect(response.body).toMatchObject({ ok: true });
    expect(response.body.image).toMatch(/^data:image\/.+;base64,abc123$/);
  });
});

describe("image generation flow", () => {
  it("uses image editing for uploaded reference images without falling back to generation", async () => {
    const generator = new OpenAIImageGenerator();
    const editSpy = vi.mocked((generator as any).client.images.edit);
    const generateSpy = vi.mocked((generator as any).client.images.generate);

    await generator.generate({
      prompt: "Edit this portrait",
      images: [
        {
          path: "test.png",
          originalname: "test.png",
          mimetype: "image/png",
          buffer: Buffer.from("fake-image-data"),
        },
      ],
    });

    expect(editSpy).toHaveBeenCalledTimes(1);
    expect(generateSpy).not.toHaveBeenCalled();
  });
});
