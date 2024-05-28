import { BodyShort, Heading, Tooltip } from "@navikt/ds-react";
import { EyeWithPupilIcon, EyeClosedIcon } from "@navikt/aksel-icons";
import { PropsWithChildren, ReactElement } from "react";

type Props = {
  title: string;
  ingress: string;
  hasAuditLog: boolean;
};

function BasicPage({
  children,
  title,
  ingress,
  hasAuditLog,
}: PropsWithChildren<Props>): ReactElement {
  return (
    <div className="pt-8">
      <Heading size="medium" level="2" className="flex gap-2 items-center">
        {title}
        {hasAuditLog ? (
          <Tooltip content="Audit-logges">
            <EyeWithPupilIcon />
          </Tooltip>
        ) : (
          <Tooltip content="Har ikke audit-log">
            <EyeClosedIcon />
          </Tooltip>
        )}
      </Heading>
      <BodyShort size="small">{ingress}</BodyShort>
      <div className="mt-8">{children}</div>
    </div>
  );
}

export default BasicPage;
