import { ReactElement, useState } from 'react'
import { Alert, Loader, TextField } from '@navikt/ds-react'
import { useQuery } from '@tanstack/react-query'

import BasicPage from '../../components/layout/BasicPage.tsx'
import FnrForm from '../person/oppslag-form/FnrForm.tsx'
import { fetchApi } from '../../api/api.ts'
import { SykmeldingsOpplysningerSchema } from '../../types/sykmeldingsOpplysningerSchema.ts'
import { raiseError } from '../../utils/ts.ts'

import SykmeldingsOpplysningerForm from './vis-sykmeldinger-form/SykmeldingsOpplysningerForm.tsx'

function SykmeldingsOpplysningerOppslag(): ReactElement {
    const [fnrToSearch, setFnrToSearch] = useState<string | null>(null)
    const [sykmeldingId, setSykmeldingId] = useState<string | null>(null)

    const { data, error, isFetching } = useQuery({
        queryKey: ['sykmeldingPerson', fnrToSearch],
        queryFn: async () => {
            if (fnrToSearch == null && sykmeldingId == null) {
                raiseError('Missing FNR or sykmeldingId')
            }
            const headers: Record<string, string> = {}
            if (fnrToSearch !== null) {
                headers['fnr'] = fnrToSearch
            } else if (sykmeldingId !== null) {
                headers['sykmeldingId'] = sykmeldingId
            }
            return await fetchApi('/sykmeldingsopplysninger', {
                schema: SykmeldingsOpplysningerSchema,
                headers: headers,
            })
        },
        enabled:
            (fnrToSearch !== null && fnrToSearch.length === 11) || (sykmeldingId !== null && sykmeldingId.length !== 0),
    })

    return (
        <BasicPage
            title="Hent sykmeldingsopplysninger"
            ingress="Hent sykmeldingsopplysninger om en person med fødselsnummer"
            hasAuditLog={true}
        >
            <TextField
                name="sykmeldingId"
                label="sykmeldingId"
                size="medium"
                onChange={(event) => {
                    setSykmeldingId(event.currentTarget.value === '' ? null : event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <FnrForm
                onChange={(fnr: string): void => {
                    setFnrToSearch(fnr === '' ? null : fnr)
                }}
            />
            {!data && !error && isFetching && <Loader size="medium" />}
            {data && <SykmeldingsOpplysningerForm person={data} sykmeldingId={sykmeldingId} />}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    )
}

export default SykmeldingsOpplysningerOppslag
