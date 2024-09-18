import {ReactElement} from "react";
import {AltinnStatusSchema} from "../../types/altinnStatusSchema.ts";

import BasicPage from "../../components/layout/BasicPage.tsx";
import {fetchApi} from "../../api/api.ts";
import AltinnStatusSykmeldingIdForm from "./sjekk-altinn-status-form/AltinnStatusSykmeldingIdForm.tsx";
import AltinnStatusForm from "./sjekk-altinn-status-form/AltinnStatusForm.tsx";
import {Alert, Loader} from "@navikt/ds-react";
import {useMutation} from "@tanstack/react-query";


function AltinnStatusOppslag(): ReactElement {
    const { data, isSuccess, isPending, error, mutate } = useMutation({
        mutationFn: async (params: { sykmeldingId: string; orgnummer: string }) => {
            if (!params.sykmeldingId) {
                throw new Error('Mangler sykmeldingId.')
            }

            if (!params.orgnummer) {
                throw new Error('Mangler journalpostId.')
            }
            return await fetchApi(`/altinnstatus/${params.sykmeldingId}/${params.orgnummer} ?? 'null'`, {
                method: 'GET',
                schema: AltinnStatusSchema,
            })
        },
    })


    return (
        <BasicPage
            title="Sjekk status i Altinn for denne sykmeldingen"
            ingress="Hent status i Altinn for en sykmelding med sykmeldingId"
            hasAuditLog={false}
        >
            <AltinnStatusSykmeldingIdForm
                onChange={(sykmeldingId: string, orgnummer: string): void => {
                    mutate({
                        sykmeldingId,
                        orgnummer,
                    })
                }}
            />
            {!data && !error && isPending && <Loader size="medium"/>}
            {data && isSuccess && <AltinnStatusForm status={data}/>}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    );
}

export default AltinnStatusOppslag;