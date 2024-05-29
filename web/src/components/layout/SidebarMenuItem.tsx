import { PropsWithChildren, ReactElement } from "react";

import { Heading } from "@navikt/ds-react";
import { PersonGroupIcon } from "@navikt/aksel-icons";
import { cn } from "../../utils/tw";

type Props = {
  title: string;
  Icon: typeof PersonGroupIcon;
  children: ReactElement;
  // temp
  todo?: true;
};

function SidebarMenuItem({
  title,
  Icon,
  children,
  todo,
}: PropsWithChildren<Props>): ReactElement {
  return (
    <div
      className={cn("pb-10", {
        "opacity-50 pointer-events-none": todo,
      })}
    >
      <Heading className="flex items-center pb-3" size="medium">
        <Icon className="mr-2" />
        {title}
      </Heading>
      <ul className="list-disc pl-12">{children}</ul>
    </div>
  );
}

export default SidebarMenuItem;
