import {ReactElement, useState} from "react";
import {AltinnStatusSchema} from "../../types/altinnStatusSchema.ts";

import BasicPage from "../../components/layout/BasicPage.tsx";
import AltinnStatusSykmeldingIdForm from "./sjekk-altinn-status-form/AltinnStatusSykmeldingIdForm.tsx";
import AltinnStatusForm from "./sjekk-altinn-status-form/AltinnStatusForm.tsx";
import { Alert, Loader } from "@navikt/ds-react";
import { useQuery } from "@tanstack/react-query";
import { fetchApi } from "../../api/api.ts";
import { raiseError } from "../../utils/ts.ts";
// la backend gjere kallet mot SyfosmAltinn, her kaller vi berre backend
function AltinnStatusOppslag(): ReactElement {
    const [sykmeldingIdToSearch, setSykmeldingIdToSearch] = useState<string | null>(null);
    const { data, error, isFetching } = useQuery({
        queryKey: ["sykmeldingId", sykmeldingIdToSearch],
        queryFn: async () =>
            fetchApi("/altinnstatus", {
                schema: AltinnStatusSchema,
                headers: { sykmeldingId: sykmeldingIdToSearch ?? raiseError("Missing sykmeldingId") },
            }),
        enabled: sykmeldingIdToSearch !== null && sykmeldingIdToSearch.length === 36,
    });

    return (
        <BasicPage
            title="Sjekk status i Altinn for denne sykmeldingen"
            ingress="Hent status i Altinn for en sykmelding med sykmeldingId"
            hasAuditLog={false}
        >
            <AltinnStatusSykmeldingIdForm
                onChange={(sykmeldingId: string): void => {
                    setSykmeldingIdToSearch(sykmeldingId);
                }}
            />
            {!data && !error && isFetching && <Loader size="medium" />}
            {data && <AltinnStatusForm status={data} />}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    );
}

export default AltinnStatusOppslag;