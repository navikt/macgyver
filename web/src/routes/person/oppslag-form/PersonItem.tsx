import { ReactElement } from "react";

import { BodyShort } from "@navikt/ds-react";

interface PersonItemProps {
  personIdentKey: string;
  value: string | boolean | null;
}

const PersonItem = ({
  personIdentKey,
  value,
}: PersonItemProps): ReactElement => {
  const val: string | null = typeof value == "boolean" ? String(value) : value;
  return (
    <li className="flex border-b border-gray-600 py-1 max-[1030px]:flex-col">
      <BodyShort className="min-w-[15rem] font-bold">
        {personIdentKey}:{" "}
      </BodyShort>
      <BodyShort>{val}</BodyShort>
    </li>
  );
};

export default PersonItem;
