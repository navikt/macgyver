import { useState } from "react";
import { Button, TextField } from "@navikt/ds-react";

interface NyNlRequestAltinnFormProps {
  onChange: (sykmeldteFnr: string) => void;
}

const NLRequestForm = ({
  onChange,
}: NyNlRequestAltinnFormProps): JSX.Element => {
  const [sykmeldteFnr, setSykmeldteFnr] = useState<string>("");

  const handleClick: React.MouseEventHandler<HTMLButtonElement> = (event) => {
    event.preventDefault();
    onChange(sykmeldteFnr);
  };
  return (
    <div>
      <TextField
        name="sykmeldteFnr"
        label="sykmeldteFnr"
        size="medium"
        onChange={(event) => {
          setSykmeldteFnr(event.currentTarget.value);
        }}
        className="my-6 w-96"
        error={
          sykmeldteFnr.length !== 11
            ? "Fødselsnummer må være 11 siffer"
            : undefined
        }
      />

      <Button
        name="hentButton"
        variant="primary"
        size="medium"
        className="my-4"
        onClick={handleClick}
      >
        Hent
      </Button>
    </div>
  );
};

export default NLRequestForm;
