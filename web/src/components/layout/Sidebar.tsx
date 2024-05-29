import { ReactElement } from "react";
import SidebarMenuItem from "./SidebarMenuItem.tsx";
import {     BandageIcon,
    FilesIcon,
    FilePdfIcon,
    ReceptionIcon,
    PersonGroupIcon,
    StethoscopeIcon,
    PersonIcon, } from "@navikt/aksel-icons";
import { Link } from "react-router-dom";

type Props = {
  className?: string;
};

function Sidebar({ className }: Props): ReactElement {
  return (
    <div className={className}>
      <SidebarMenuItem todo title="IdentEndring" Icon={PersonGroupIcon}>
        <li>
          <Link to="/ident-endring">Endre fnr for en gitt sykmelding</Link>
        </li>
      </SidebarMenuItem>
      <SidebarMenuItem todo title="Sykmelding" Icon={BandageIcon}>
        <li>
          <Link to="/slett-sykmelding">Slett en gitt sykmelding</Link>
        </li>
      </SidebarMenuItem>
      <SidebarMenuItem title="Oppgave" Icon={FilesIcon}>
        <li>
          <Link to="/oppgave/oppslag">Hent oppgaver</Link>
        </li>
      </SidebarMenuItem>
      <SidebarMenuItem todo title="Narmesteleder" Icon={ReceptionIcon}>
        <>
          <li>
            <Link to="/ny-naermeste-leder">
              Sender ny NL-request til altinn
            </Link>
          </li>
          <li>
            <Link to="/hent-naermeste-ledere">
              Hent narmesteldere for ein sykmeldt person
            </Link>
          </li>
        </>
      </SidebarMenuItem>
      <SidebarMenuItem todo title="Legeerklæring" Icon={StethoscopeIcon}>
        <li>
          <Link to="/slett-legeerklaering">Slett en gitt legeerklæring</Link>
        </li>
      </SidebarMenuItem>
      <SidebarMenuItem todo title="Journalpost" Icon={FilePdfIcon}>
        <li>
          <Link to="/hent-journalposter">Hent liste med journalposter</Link>
        </li>
      </SidebarMenuItem>
      <SidebarMenuItem title="Person" Icon={PersonIcon}>
        <li>
          <Link to="/person/oppslag">
            Hent navn på person og liste med identer
          </Link>
        </li>
      </SidebarMenuItem>
    </div>
  );
}

export default Sidebar;
