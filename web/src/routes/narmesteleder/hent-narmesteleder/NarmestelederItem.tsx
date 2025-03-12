import { ReactElement } from 'react'
import { Alert } from '@navikt/ds-react'

import { Narmesteleder } from '../../../types/narmesteleder.ts'

import NarmesteledereListItem from './NarmesteledereListItem'

interface NarmesteledereListProps {
    narmesteleder: Narmesteleder
}

const NarmestelederItem = ({ narmesteleder }: NarmesteledereListProps): ReactElement => {
    return (
        <Alert className="items-start" variant="success">
            <ul>
                {Object.entries(narmesteleder).map(([key, value]) => (
                    <NarmesteledereListItem key={narmesteleder.fnr + key} narmesteLedereKey={key} value={value} />
                ))}
            </ul>
        </Alert>
    )
}

export default NarmestelederItem
