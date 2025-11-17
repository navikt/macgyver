import { ReactElement, useState } from 'react'
import { Alert, Loader } from '@navikt/ds-react'
import { useQuery } from '@tanstack/react-query'

import BasicPage from '../../components/layout/BasicPage.tsx'
import { fetchApi } from '../../api/api.ts'
import { PersonSchema } from '../../types/personSchema.ts'
import { raiseError } from '../../utils/ts.ts'

import PersonWithIdent from './oppslag-form/PersonWithIdent.tsx'
import FnrForm from './oppslag-form/FnrForm.tsx'

function PersonOppslag(): ReactElement {
    const [fnrToSearch, setFnrToSearch] = useState<string | null>(null)
    const { data, error, isFetching, refetch } = useQuery({
        queryKey: ['person', fnrToSearch],
        queryFn: async () =>
            fetchApi('/person', {
                schema: PersonSchema,
                headers: { fnr: fnrToSearch ?? raiseError('Missing FNR') },
            }),
        enabled: false,
    })

    return (
        <BasicPage
            title="Oppslag på person"
            ingress="Hent navn på person og liste med identer fra saf-api"
            hasAuditLog={false}
        >
            <FnrForm
                onChange={(fnr: string): void => {
                    setFnrToSearch(fnr)
                }}
                refetch={refetch}
            />
            {!data && !error && isFetching && <Loader size="medium" />}
            {data && <PersonWithIdent person={data} />}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    )
}

export default PersonOppslag
