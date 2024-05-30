import { ReactElement, useState } from "react";
import { Alert, Loader } from "@navikt/ds-react";
import { z } from "zod";
import { useQuery } from "@tanstack/react-query";

import { OppgaveSchema } from "../../types/oppgaver.ts";
import BasicPage from "../../components/layout/BasicPage";
import { fetchApi } from "../../api/api.ts";

import OppgaveIdForm from "./oppgaveid-form/OppgaveIdForm.tsx";
import OppgaveList from "./oppgaveid-form/OppgaveList.tsx";

function OppgaveOppslag(): ReactElement {
  const [oppgaveIds, setOppgaveIds] = useState<number[]>([]);
  const { data, error, isFetching } = useQuery({
    queryKey: ["oppgaver", ...oppgaveIds],
    queryFn: async () =>
      fetchApi("/oppgave/list", {
        method: "POST",
        schema: z.array(OppgaveSchema),
        body: oppgaveIds,
      }),
    enabled: oppgaveIds.length > 0,
  });

  return (
    <BasicPage
      title="Hent oppgaver"
      ingress="Hent en liste med oppgaver med oppgaveId fra Oppgave-api: eks: 2, 3, 4, 5"
      hasAuditLog
    >
      <OppgaveIdForm
        onChange={(oppgaveIds: number[]): void => {
          setOppgaveIds(oppgaveIds);
        }}
      />
      {!data && !error && isFetching && <Loader size="medium" />}
      {data && (
        <div className="flex flex-wrap gap-6">
          {data.map((oppgave) => (
            <OppgaveList key={oppgave.id} oppgave={oppgave} />
          ))}
        </div>
      )}
      {error && <Alert variant="error">{error.message}</Alert>}
    </BasicPage>
  );
}

export default OppgaveOppslag;
