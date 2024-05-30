import { ReactElement } from "react";
import { BodyShort } from "@navikt/ds-react";

interface OppgaveListItemProps {
  oppgaveKey: string;
  value: string | number | null;
}

const OppgaveListItem = ({
  oppgaveKey,
  value,
}: OppgaveListItemProps): ReactElement => {
  return (
    <li className="flex border-b border-gray-600 py-1 max-[1030px]:flex-col">
      <BodyShort className="min-w-[15rem] font-bold">{oppgaveKey}: </BodyShort>
      <BodyShort>{value}</BodyShort>
    </li>
  );
};

export default OppgaveListItem;
