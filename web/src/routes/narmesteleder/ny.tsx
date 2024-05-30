import { ReactElement } from "react";
import { useMutation } from "@tanstack/react-query";
import { Alert, BodyShort, Loader } from "@navikt/ds-react";

import BasicPage from "../../components/layout/BasicPage";
import { MessageSchema } from "../../api/message";
import { fetchApi } from "../../api/api";
import { NyNLAltinnSchema } from "../../types/nyNLAltinn";

import NyNLRequestAltinnForm from "./ny-nl-request-altinn-form/NyNLRequestAltinnForm";

function NyNarmesteleder(): ReactElement {
  const { data, isSuccess, isPending, error, mutate } = useMutation({
    mutationFn: async (params: {
      sykmeldingId: string;
      fnr: string;
      orgnummer: string;
    }) => {
      const payload = NyNLAltinnSchema.parse(params);

      return await fetchApi(`/narmesteleder/request`, {
        method: "POST",
        schema: MessageSchema,
        body: payload,
      });
    },
  });

  return (
    <BasicPage
      title="NL-request til Altinn"
      ingress="Sender ny NL-request til Altinn"
      hasAuditLog
    >
      <NyNLRequestAltinnForm
        onChange={(
          sykmeldingId: string,
          fnr: string,
          orgnummer: string,
        ): void => {
          mutate({ sykmeldingId, fnr, orgnummer });
        }}
      />
      {isPending && !error && <Loader size="medium" />}
      {isSuccess && (
        <Alert variant="success">
          <BodyShort spacing>Ny NL-request er sendt til altinn.</BodyShort>
          <BodyShort size="small">MacGyver sa: {data.message}</BodyShort>
        </Alert>
      )}
      {error && <Alert variant="error">{error.message}</Alert>}
    </BasicPage>
  );
}

export default NyNarmesteleder;
