import { ReactElement } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Alert, BodyShort, Loader } from '@navikt/ds-react'

import BasicPage from '../../components/layout/BasicPage.tsx'
import { fetchApi } from '../../api/api.ts'
import { MessageSchema } from '../../api/message.ts'

import DeaktiverNlForm from './deaktiver-nl-form/DeaktiverNlForm.tsx'

function DeaktiverNarmesteleder(): ReactElement {
    const { isSuccess, isPending, error, mutate } = useMutation({
        mutationFn: async (params: { id: string; fnr: string; orgnummer: string }) => {
            return await fetchApi(`/narmesteleder/${params.id}`, {
                method: 'DELETE',
                headers: {
                    orgnummer: params.orgnummer,
                    fnr: params.fnr,
                },
                schema: MessageSchema,
            })
        },
    })

    return (
        <BasicPage title="Deaktiver NL-kobling" ingress="Deaktiver en NL kobling" hasAuditLog>
            <DeaktiverNlForm
                onChange={(id: string, fnr: string, orgnummer: string): void => {
                    mutate({ id, fnr, orgnummer })
                }}
            />
            {isPending && !error && <Loader size="medium" />}
            {isSuccess && (
                <Alert variant="success">
                    <BodyShort spacing>Deaktivert NL-kobling.</BodyShort>
                </Alert>
            )}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    )
}

export default DeaktiverNarmesteleder
