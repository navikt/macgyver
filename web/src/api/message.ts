import { z } from "zod";

export const MessageSchema = z.object({
  message: z.string(),
});
