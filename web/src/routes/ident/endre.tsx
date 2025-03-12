import { ReactElement } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Alert, BodyShort, Loader } from '@navikt/ds-react'

import BasicPage from '../../components/layout/BasicPage.tsx'
import { fetchApi } from '../../api/api.ts'
import { IdentEndringSykmeldtPayload, IdentEndringSykmeldtPayloadSchema } from '../../types/identEndring.ts'
import { MessageSchema } from '../../api/message.ts'

import IdentEndringSykmeldtForm from './ident-endring-form/IdentEndringSykmeldtForm.tsx'

function EndreIdent(): ReactElement {
    const { data, isSuccess, isPending, error, mutate } = useMutation({
        mutationFn: async (data: IdentEndringSykmeldtPayload) => {
            const payload = IdentEndringSykmeldtPayloadSchema.parse(data)

            return await fetchApi('/sykmelding/fnr', {
                method: 'POST',
                body: payload,
                schema: MessageSchema,
            })
        },
    })

    return (
        <BasicPage
            title="IdentEndring"
            ingress="Endrer fnr for ein sykmeldt person i alle sykmeldinger i SyfoSmRegister og oppdaterer aktive NL-koblinger."
            hasAuditLog
        >
            <IdentEndringSykmeldtForm
                onChange={(fnr: string, nyttFnr: string): void => {
                    mutate({ fnr, nyttFnr })
                }}
            />
            {isPending && !error && <Loader size="medium" />}
            {isSuccess && (
                <Alert variant="success">
                    <BodyShort spacing>Endring av fnr for sykmeldt er fullført.</BodyShort>
                    <BodyShort size="small">MacGyver sa: {data.message}</BodyShort>
                </Alert>
            )}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    )
}

export default EndreIdent
