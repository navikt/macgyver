import { ReactElement } from 'react'
import { BodyShort } from '@navikt/ds-react'

interface JournalpostListItemProps {
    journalpostKey: string
    value: string | number | boolean | null
}

const JournalpostListItem = ({ journalpostKey, value }: JournalpostListItemProps): ReactElement => {
    return (
        <li className="flex border-b border-gray-600 py-1 max-[1030px]:flex-col">
            <BodyShort className="min-w-[10rem] font-bold">{journalpostKey}: </BodyShort>
            <BodyShort>{value}</BodyShort>
        </li>
    )
}

export default JournalpostListItem
