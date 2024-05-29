import { ReactElement } from "react";
import SidebarMenuItem from "./SidebarMenuItem.tsx";
import { FilesIcon, PersonIcon } from "@navikt/aksel-icons";
import { Link } from "react-router-dom";

type Props = {
  className?: string;
};

function Sidebar({ className }: Props): ReactElement {
  return (
    <div className={className}>
      <SidebarMenuItem title="Oppgave" Icon={FilesIcon}>
        <li>
          <Link to="/oppgave/oppslag">Hent oppgaver</Link>
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
