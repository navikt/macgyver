import { ReactElement } from 'react'
import { Alert } from '@navikt/ds-react'

import { Oppgave } from '../../../types/oppgaver'

import OppgaveListItem from './OppgaveListItem'

interface OppgaveListProps {
    oppgave: Oppgave
}

const OppgaveList = ({ oppgave }: OppgaveListProps): ReactElement => {
    return (
        <Alert key={oppgave.id} className="items-start max-w-prose" variant="success">
            <ul>
                {Object.entries(oppgave).map(([key, value]) => (
                    <OppgaveListItem key={oppgave.id + key} oppgaveKey={key} value={value} />
                ))}
            </ul>
        </Alert>
    )
}

export default OppgaveList
