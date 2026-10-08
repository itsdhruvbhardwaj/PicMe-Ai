import mongoose, { Schema } from "mongoose";

const GenerationSchema = new Schema(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      index: true,
    },
    styleId: {
      type: String,
      required: true,
    },
    imageUrl: {
      type: String,
      default: "",
    },
    thumbnailUrl: {
      type: String,
      default: "",
    },
    status: {
      type: String,
      enum: ["processing", "completed", "failed"],
      default: "processing",
    },
  },
  { timestamps: true },
);

GenerationSchema.index({ userId: 1, createdAt: -1 });

export const Generation = mongoose.models.Generation || mongoose.model("Generation", GenerationSchema);
