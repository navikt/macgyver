import { z, ZodTypeAny } from "zod";

type KnownPaths =
  | "/person"
  | "/oppgave/list"
  | "/narmesteleder"
  | "/sykmelding/fnr"
  | (string & {});

export async function fetchApi<SchemaType extends ZodTypeAny>(
  path: KnownPaths,
  {
    method,
    body,
    schema,
    headers,
  }: {
    method?: "GET" | "POST" | "DELETE";
    headers?: Record<string, string>;
    body?: unknown;
    schema: SchemaType;
  },
): Promise<z.infer<SchemaType>> {
  const response = await fetch(`/api${path}`, {
    method: method ?? "GET",
    headers: {
      "Content-Type": "application/json",
      ...headers,
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    throw new Error(
      `Ktor backend not happy :( It said ${response.statusText} (${response.status})`,
    );
  }

  try {
    const data = await response.json();

    return schema.parse(data);
  } catch (e) {
    console.error(e);
    throw new Error(`Failed to parse response from Ktor backend: ${e}`);
  }
}
