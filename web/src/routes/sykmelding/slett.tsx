import { ReactElement } from "react";
import { useMutation } from "@tanstack/react-query";
import { Alert, BodyShort, Loader } from "@navikt/ds-react";

import BasicPage from "../../components/layout/BasicPage";
import { fetchApi } from "../../api/api.ts";
import { MessageSchema } from "../../api/message.ts";

import SlettSykmeldingForm from "./slett-sykmelding-form/SlettSykmeldingForm";

function SlettSykmelding(): ReactElement {
  const { data, isSuccess, isPending, error, mutate } = useMutation({
    mutationFn: async (params: {
      sykmeldingId: string;
      journalpostId: string | null;
    }) => {
      if (!params.sykmeldingId) {
        throw new Error("Mangler sykmeldingId.");
      }

      if (!params.journalpostId) {
        throw new Error("Mangler journalpostId.");
      }

      return await fetchApi(
        `/sykmelding/${params.sykmeldingId}/${params.journalpostId ?? "null"}`,
        {
          method: "DELETE",
          schema: MessageSchema,
        },
      );
    },
  });

  return (
    <BasicPage
      title="Slett sykmelding"
      ingress="Sletter en sykmelding"
      hasAuditLog
    >
      <SlettSykmeldingForm
        onSubmit={(sykmeldingId: string, journalpostId: string): void => {
          mutate({
            sykmeldingId,
            journalpostId,
          });
        }}
      />
      {isPending && !error && <Loader size="medium" />}
      {isSuccess && (
        <Alert variant="success">
          <BodyShort spacing>Sykmeldingen er slettet.</BodyShort>
          <BodyShort size="small">MacGyver sa: {data.message}</BodyShort>
        </Alert>
      )}
      {error && <Alert variant="error">{error.message}</Alert>}
    </BasicPage>
  );
}

export default SlettSykmelding;
