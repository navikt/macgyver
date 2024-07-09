import { ReactElement } from "react";
import { BodyShort } from "@navikt/ds-react";

interface NarmesteldereListProps {
  narmesteLedereKey: string;
  value: string | number | boolean | null;
}

const NarmesteledereListItem = ({
  narmesteLedereKey,
  value,
}: NarmesteldereListProps): ReactElement => {
  const val: string | number | null =
    typeof value == "boolean" ? String(value) : value;

  return (
    <li className="flex border-b border-gray-600 py-1 max-[1130px]:flex-col">
      <BodyShort className="min-w-[18rem] font-bold">
        {narmesteLedereKey}:{" "}
      </BodyShort>
      <BodyShort>{val}</BodyShort>
    </li>
  );
};

export default NarmesteledereListItem;
