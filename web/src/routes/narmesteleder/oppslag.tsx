import { ReactElement, useState } from "react";
import { z } from "zod";
import { useQuery } from "@tanstack/react-query";
import { Alert, Loader } from "@navikt/ds-react";

import BasicPage from "../../components/layout/BasicPage";
import { fetchApi } from "../../api/api";
import { raiseError } from "../../utils/ts";
import { Narmesteleder, NarmestelederSchema } from "../../types/narmesteleder";

import NarmestelederItem from "./hent-narmesteleder/NarmestelederItem.tsx";
import NLRequestForm from "./hent-narmesteleder/NLRequestForm";

function OppslagNarmesteleder(): ReactElement {
  const [fnrToSearch, setFnrToSearch] = useState<string | null>(null);
  const { data, error, isFetching } = useQuery({
    queryKey: ["person", fnrToSearch],
    queryFn: async () =>
      fetchApi("/narmesteleder", {
        schema: z.array(NarmestelederSchema),
        headers: { fnr: fnrToSearch ?? raiseError("Missing FNR") },
      }),
    enabled: fnrToSearch !== null && fnrToSearch.length === 11,
  });

  return (
    <BasicPage
      title="Nærmesteledere for sykmeldt"
      ingress="Henting narmesteledere for ein sykmeldt person"
      hasAuditLog
    >
      <NLRequestForm
        onChange={(sykmeldtFnr: string): void => {
          setFnrToSearch(sykmeldtFnr);
        }}
      />
      {!data && !error && isFetching && <Loader size="medium" />}
      {data && (
        <div className="flex flex-wrap gap-6">
          {data.map((narmesteledere: Narmesteleder) => (
            <NarmestelederItem
              key={narmesteledere.fnr}
              narmesteleder={narmesteledere}
            />
          ))}
        </div>
      )}
      {error && <Alert variant="error">{error.message}</Alert>}
    </BasicPage>
  );
}

export default OppslagNarmesteleder;
