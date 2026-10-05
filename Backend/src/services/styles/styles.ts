export interface Style {
  id: string;
  name: string;
  description: string;
  prompt: string;
  thumbnail: null;
}

export const styles: Style[] = [
  {
    id: "cinematic-couple",
    name: "Cinematic Couple",
    description: "Create a cinematic couple portrait",
    prompt: "Create a cinematic, emotionally warm couple portrait with refined lighting and a natural composition.",
    thumbnail: null,
  },
  {
    id: "90s-bollywood",
    name: "90s Bollywood",
    description: "A colorful, nostalgic 1990s Bollywood-inspired portrait",
    prompt: "Apply a colorful, nostalgic 1990s Bollywood film aesthetic with expressive cinematic lighting and period-inspired color grading.",
    thumbnail: null,
  },
  {
    id: "vintage-film",
    name: "Vintage Film",
    description: "A timeless analog film portrait",
    prompt: "Create a timeless analog film portrait with subtle grain, gentle contrast, natural skin tones, and restrained vintage color.",
    thumbnail: null,
  },
  {
    id: "anime-portrait",
    name: "Anime Portrait",
    description: "A polished anime-inspired portrait",
    prompt: "Render the portrait in a polished anime-inspired illustration style while preserving the subject's recognizable facial characteristics.",
    thumbnail: null,
  },
  {
    id: "wedding",
    name: "Wedding",
    description: "An elegant, romantic wedding portrait",
    prompt: "Create an elegant, romantic wedding portrait with graceful composition, soft natural light, and refined editorial styling.",
    thumbnail: null,
  },
  {
    id: "professional-portrait",
    name: "Professional Portrait",
    description: "A polished professional headshot",
    prompt: "Create a polished professional portrait with flattering studio-quality light, a clean background, and natural, credible styling.",
    thumbnail: null,
  },
];

export function findStyle(styleId: string): Style | undefined {
  return styles.find((style) => style.id === styleId);
}

export function buildPrompt(style: Style, imageCount: number): string {
  const referenceInstructions = imageCount === 1
    ? "Use the uploaded image as the primary subject reference. Preserve the person's identity and recognizable facial characteristics. Avoid unnecessary changes to identity."
    : "Use the first reference image as the base scene and primary composition. Naturally incorporate the person from the second reference image into that scene. Preserve the recognizable facial characteristics and identity of both people. Do not simply place the two images side by side or concatenate them."

  return [
    referenceInstructions,
    style.prompt,
    "Apply the selected visual style to the composition while keeping the result realistic and high quality. Follow the reference composition unless the selected style requires a restrained adjustment. Do not identify or infer the identity of any person.",
  ].join(" ");
}