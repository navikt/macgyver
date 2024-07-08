import { ReactElement } from "react";
import { BodyShort } from "@navikt/ds-react";

interface NarmesteLedereListProps {
  narmesteledereKey: string;
  value: string | number | boolean | null;
}

const NarmesteLedereListItem = ({
  narmesteledereKey,
  value,
}: NarmesteLedereListProps): ReactElement => {
  const val: string | number | null =
    typeof value == "boolean" ? String(value) : value;

  return (
    <li className="flex border-b border-gray-600 py-1 max-[1130px]:flex-col">
      <BodyShort className="min-w-[18rem] font-bold">
        {narmesteledereKey}:{" "}
      </BodyShort>
      <BodyShort>{val}</BodyShort>
    </li>
  );
};

export default NarmesteLedereListItem;
