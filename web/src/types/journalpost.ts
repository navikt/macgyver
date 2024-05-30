import { z } from "zod";

export const JournalpostSchema = z.object({
  journalpostId: z.string(),
  tittel: z.string(),
});

export type Journalpost = z.infer<typeof JournalpostSchema>;
