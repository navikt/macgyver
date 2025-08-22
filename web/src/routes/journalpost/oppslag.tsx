import { ReactElement, useState } from 'react'
import { Alert, Loader } from '@navikt/ds-react'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'

import BasicPage from '../../components/layout/BasicPage'
import FnrForm from '../person/oppslag-form/FnrForm'
import { fetchApi } from '../../api/api'
import { raiseError } from '../../utils/ts'
import JournalpostList from '../person/oppslag-form/JournalpostList'
import { JournalpostSchema } from '../../types/journalpost.ts'

function JournalposterOppslag(): ReactElement {
    const [fnrToSearch, setFnrToSearch] = useState<string | null>(null)
    const { data, error, isFetching, refetch } = useQuery({
        queryKey: ['person', fnrToSearch],
        queryFn: async () =>
            fetchApi('/journalposter', {
                schema: z.array(JournalpostSchema),
                headers: { fnr: fnrToSearch ?? raiseError('Missing FNR') },
            }),
        enabled: false,
    })

    return (
        <BasicPage
            title="Oppslag mange journalposter"
            ingress="Hent en liste med journalposter, med oppgaveId fra saf-api"
            hasAuditLog
        >
            <FnrForm onChange={setFnrToSearch} refetch={refetch} />
            {!data && !error && isFetching && <Loader size="medium" />}
            {data && <JournalpostList journalpostLister={data} />}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    )
}

export default JournalposterOppslag
