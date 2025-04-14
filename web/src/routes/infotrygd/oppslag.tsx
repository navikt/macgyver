import { ReactElement } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Alert, Loader } from '@navikt/ds-react'

import { fetchApi } from '../../api/api.ts'
import { InfotrygdQueryType, InfotrygdResponse } from '../../types/infotrygd.ts'
import BasicPage from '../../components/layout/BasicPage.tsx'

import InfotrygdForm from './InfotrygdForm.tsx'
import { InfotrygdResponseView } from './InfotrygdRespons.tsx'

function InfotrygdOppslag(): ReactElement {
    const { data, isSuccess, isPending, error, mutate } = useMutation({
        mutationFn: async (data: InfotrygdQueryType) => {
            if (!data.ident) {
                throw new Error('Mangler ident.')
            }

            return await fetchApi(`/altinnstatus`, {
                method: 'GET',
                schema: InfotrygdResponse,
                body: data,
            })
        },
    })

    return (
        <BasicPage title="Infotrygd" ingress="Gjør oppslag for infotrygd for å sjekke identdato" hasAuditLog>
            <InfotrygdForm
                onChange={(
                    ident: string,
                    tkNummer: string | null,
                    hoveddiagnose: string | null,
                    hoveddiagnoseKodeSystem: string | null,
                    bidiagnose: string | null,
                    bidiagnoseKodeSystem: string | null,
                    identBehandler: string | null,
                ): void => {
                    mutate({
                        ident,
                        tkNummer,
                        hoveddiagnose,
                        hoveddiagnoseKodeSystem,
                        bidiagnose,
                        bidiagnoseKodeSystem,
                        identBehandler,
                    })
                }}
            ></InfotrygdForm>
            {isPending && !error && <Loader size="medium" />}
            {isSuccess && <InfotrygdResponseView response={data}></InfotrygdResponseView>}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    )
}

export default InfotrygdOppslag
