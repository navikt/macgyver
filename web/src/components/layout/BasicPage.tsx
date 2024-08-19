import { BodyShort, Heading, Tooltip } from '@navikt/ds-react'
import { EyeWithPupilIcon, EyeClosedIcon } from '@navikt/aksel-icons'
import { PropsWithChildren, ReactElement } from 'react'

type Props = {
    title: string
    ingress: string
    hasAuditLog: boolean | 'irrelevant'
}

function BasicPage({ children, title, ingress, hasAuditLog }: PropsWithChildren<Props>): ReactElement {
    return (
        <div>
            <Heading size="medium" level="2" className="flex gap-2 items-center">
                {title}
                {hasAuditLog !== 'irrelevant' && <HasAuditLog yes={hasAuditLog} />}
            </Heading>
            <BodyShort size="small">{ingress}</BodyShort>
            <div className="mt-8">{children}</div>
        </div>
    )
}

function HasAuditLog({ yes }: { yes: boolean }): ReactElement {
    if (yes) {
        return (
            <Tooltip content="Auditlogges">
                <EyeWithPupilIcon />
            </Tooltip>
        )
    }

    return (
        <Tooltip content="Har ikke audit-log">
            <EyeClosedIcon />
        </Tooltip>
    )
}

export default BasicPage
