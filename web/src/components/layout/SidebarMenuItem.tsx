import { PropsWithChildren, ReactElement } from 'react'

import { Heading } from '@navikt/ds-react'
import { PersonGroupIcon } from '@navikt/aksel-icons'

type Props = {
    title: string
    Icon: typeof PersonGroupIcon
    children: ReactElement
}

function SidebarMenuItem({ title, Icon, children }: PropsWithChildren<Props>): ReactElement {
    return (
        <div className="pb-10">
            <Heading className="flex items-center pb-3" size="large">
                <Icon className="relative br-10" />
                {title}
            </Heading>
            <ul className="list-disc pl-12">{children}</ul>
        </div>
    )
}

export default SidebarMenuItem
