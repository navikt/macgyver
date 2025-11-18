import { ReactElement } from 'react'
import {
    BandageIcon,
    FilesIcon,
    FilePdfIcon,
    ReceptionIcon,
    PersonGroupIcon,
    StethoscopeIcon,
    PersonIcon,
} from '@navikt/aksel-icons'
import { Link } from 'react-router-dom'

import SidebarMenuItem from './SidebarMenuItem.tsx'

type Props = {
    className?: string
}

function Sidebar({ className }: Props): ReactElement {
    return (
        <div className={className}>
            <SidebarMenuItem title="IdentEndring" Icon={PersonGroupIcon}>
                <li>
                    <Link to="/ident/endre">Endre fnr for en gitt sykmelding</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Sykmelding" Icon={BandageIcon}>
                <li>
                    <Link to="/sykmelding/slett">Slett en gitt sykmelding</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Oppgave" Icon={FilesIcon}>
                <li>
                    <Link to="/oppgave/oppslag">Hent oppgaver</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Narmesteleder" Icon={ReceptionIcon}>
                <li>
                    <Link to="/narmesteleder/ny">Sender ny NL-request til altinn</Link>
                </li>
                <li>
                    <Link to="/narmesteleder/oppslag">Hent nærmesteledere for en sykmeldt</Link>
                </li>
                <li>
                    <Link to="/narmesteleder/leder">Hent nærmestelederkoblinger for en leder</Link>
                </li>
                <li>
                    <Link to="/narmesteleder/deaktiver">Deaktiver narmesteleder kobling</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Legeerklæring" Icon={StethoscopeIcon}>
                <li>
                    <Link to="/legeerklaering/slett">Slett en gitt legeerklæring</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Journalpost" Icon={FilePdfIcon}>
                <li>
                    <Link to="/journalpost/oppslag">Hent liste med journalposter</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Person" Icon={PersonIcon}>
                <li>
                    <Link to="/person/oppslag">Hent navn på person og liste med identer</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Sykmeldingsopplysninger" Icon={BandageIcon}>
                <li>
                    <Link to="/sykmeldingsopplysninger/oppslag">Hent sykmeldingsopplysninger på en person</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="AltinnStatus" Icon={PersonIcon}>
                <li>
                    <Link to="/altinnstatus/oppslag">Hent status fra Altinn på en gitt sykmelding og orgnummer</Link>
                </li>
            </SidebarMenuItem>
            <SidebarMenuItem title="Infotrygd" Icon={PersonIcon}>
                <li>
                    <Link to="/infotrygd/oppslag">Send Infotrygdforespørsel</Link>
                </li>
            </SidebarMenuItem>
        </div>
    )
}

export default Sidebar
