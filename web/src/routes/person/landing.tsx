import { ReactElement } from 'react'
import { BodyShort } from '@navikt/ds-react'

import BasicPage from '../../components/layout/BasicPage.tsx'

function Landing(): ReactElement {
    return (
        <BasicPage
            title="Velkommen til MacGyver for Team Sykmelding!"
            ingress="Vårt interne verktøy for å feilsøke og fikse data i produksjon"
            hasAuditLog="irrelevant"
        >
            <BodyShort>Velg en funksjon i venstremenyen for å komme i gang</BodyShort>
        </BasicPage>
    )
}

export default Landing
