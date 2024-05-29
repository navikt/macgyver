import { ReactElement } from "react";

import image from "./images/not-found-image.webp";
import { BodyShort, Heading } from "@navikt/ds-react";

function NotFound(): ReactElement {
  return (
    <div className="flex justify-center items-center h-full">
      <div className="flex flex-col justify-center items-center">
        <img src={image} height={256} width={256} />
        <Heading size="large" level="2">
          Fant ikke siden!
        </Heading>
        <BodyShort>Ikke en gang MacGyver klarer å fikse dette</BodyShort>
      </div>
    </div>
  );
}

export default NotFound;
