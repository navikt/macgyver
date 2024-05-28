import { z, ZodTypeAny } from "zod";

export async function fetchApi<SchemaType extends ZodTypeAny>(
  path: string,
  {
    method,
    schema,
    headers,
  }: {
    method?: "GET";
    headers?: Record<string, string>;
    schema: SchemaType;
  },
): Promise<z.infer<SchemaType>> {
  const response = await fetch(path, {
    method: method ?? "GET",
    headers: {
      "Content-Type": "application/json",
      ...headers,
    },
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
