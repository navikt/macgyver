import { ReactElement } from "react";
import BasicPage from "../../components/layout/BasicPage.tsx";
import { useMutation } from "@tanstack/react-query";
import { Alert, BodyShort, Loader } from "@navikt/ds-react";
import { fetchApi } from "../../api/api.ts";
import { MessageSchema } from "../../api/message.ts";
import SlettLegeerklaeringForm from "./slett-legeerklaering-form/SlettLegeerklaeringForm.tsx";

function SlettLegeerklaering(): ReactElement {
  const { data, isSuccess, isPending, error, mutate } = useMutation({
    mutationFn: async (legeerklaeringId: string) => {
      if (!legeerklaeringId) throw new Error("Mangler legeerklaeringId.");

      return await fetchApi(`/legeerklaering/${legeerklaeringId}`, {
        method: "DELETE",
        schema: MessageSchema,
        body: { legeerklaeringId },
      });
    },
  });

  return (
    <BasicPage
      title="Slett legeerklæring"
      ingress="Sletter en legeerklæring"
      hasAuditLog
    >
      <SlettLegeerklaeringForm
        onSubmit={(legeerklaeringId: string): void => {
          mutate(legeerklaeringId);
        }}
      />
      {isPending && !error && <Loader size="medium" />}
      {isSuccess && (
        <Alert variant="success">
          <BodyShort spacing>Legeerklæring er slettet.</BodyShort>
          <BodyShort size="small">MacGyver sa: {data.message}</BodyShort>
        </Alert>
      )}
      {error && <Alert variant="error">{error.message}</Alert>}
    </BasicPage>
  );
}

export default SlettLegeerklaering;
