import { ReactElement, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import { Alert, Loader } from '@navikt/ds-react'

import { fetchApi } from '../../api/api.ts'
import { Narmesteleder, NarmestelederSchema } from '../../types/narmesteleder.ts'
import { raiseError } from '../../utils/ts.ts'
import BasicPage from '../../components/layout/BasicPage.tsx'

import NLRequestForm from './hent-narmesteleder/NLRequestForm.tsx'
import NarmestelederItem from './hent-narmesteleder/NarmestelederItem.tsx'

function LederOppslag(): ReactElement {
    const [fnrToSearch, setFnrToSearch] = useState<string | null>(null)
    const { data, error, isFetching } = useQuery({
        queryKey: ['narmestelederLeder', fnrToSearch],
        queryFn: async () =>
            fetchApi('/narmesteleder/leder', {
                schema: z.array(NarmestelederSchema),
                headers: { fnr: fnrToSearch ?? raiseError('Missing FNR') },
            }),
        enabled: fnrToSearch !== null && fnrToSearch.length === 11,
    })

    return (
        <BasicPage
            title="Nærmestelederkoblinger for leder"
            ingress="Hent narmestelederekoblinger for en leder"
            hasAuditLog
        >
            <NLRequestForm
                onChange={(sykmeldtFnr: string): void => {
                    setFnrToSearch(sykmeldtFnr)
                }}
            />
            {!data && !error && isFetching && <Loader size="medium" />}
            {data && (
                <div className="flex flex-wrap gap-6">
                    {data.map((narmesteledere: Narmesteleder) => (
                        <NarmestelederItem key={narmesteledere.fnr} narmesteleder={narmesteledere} />
                    ))}
                </div>
            )}
            {error && <Alert variant="error">{error.message}</Alert>}
        </BasicPage>
    )
}

export default LederOppslag
