import { ReactElement, useState } from 'react'

import BasicPage from '../../components/layout/BasicPage.tsx'
import FnrForm from '../person/oppslag-form/FnrForm.tsx'
import { Alert, Loader } from '@navikt/ds-react'
import { useQuery } from '@tanstack/react-query'
import { fetchApi } from '../../api/api.ts'
import { SykmeldingsOpplysningerSchema } from '../../types/sykmeldingsOpplysningerSchema.ts'
import { raiseError } from '../../utils/ts.ts'
import SykmeldingsOpplysningerForm from './vis-sykmeldinger-form/SykmeldingsOpplysningerForm.tsx'

function SykmeldingsOpplysningerOppslag(): ReactElement {
    const [fnrToSearch, setFnrToSearch] = useState<string | null>(null)
    const { data, error, isFetching } = useQuery({
        queryKey: ['sykmeldingPerson', fnrToSearch],
        queryFn: async () =>
            fetchApi('/sykmeldingsopplysninger', {
                schema: SykmeldingsOpplysningerSchema,
                headers: { fnr: fnrToSearch ?? raiseError('Missing FNR') },
            }),
        enabled: fnrToSearch !== null && fnrToSearch.length === 11,
    })

    return (
        <BasicPage
            title="Hent sykmeldingsopplysninger"
            ingress="Hent sykmeldingsopplysninger om en person med fødselsnummer"
            hasAuditLog={true}
        >
            <FnrForm
                onChange={(fnr: string): void => {
                    setFnrToSearch(fnr)
                }}
            />
            {!data && !error && isFetching && <Loader size="medium" />}
            {data && <SykmeldingsOpplysningerForm person={data} />}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    )
}

export default SykmeldingsOpplysningerOppslag
