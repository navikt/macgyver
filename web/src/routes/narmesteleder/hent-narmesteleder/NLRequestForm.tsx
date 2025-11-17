import { ReactElement, useState } from 'react'
import { Button, TextField } from '@navikt/ds-react'

interface NyNlRequestAltinnFormProps {
    onChange: (sykmeldteFnr: string) => void
}

const NLRequestForm = ({ onChange }: NyNlRequestAltinnFormProps): ReactElement => {
    const [fnr, setSykmeldteFnr] = useState<string>('')
    const [error, setError] = useState<string | null>(null)

    const handleClick: React.MouseEventHandler<HTMLButtonElement> = (event) => {
        event.preventDefault()
        onChange(fnr)

        if (fnr.length !== 11) {
            setError('Fødselsnummer må være 11 siffer')
        } else {
            setError(null)
        }
    }
    return (
        <div>
            <TextField
                name="fnr"
                label="fnr"
                size="medium"
                onChange={(event) => {
                    setSykmeldteFnr(event.currentTarget.value)
                }}
                className="my-6 w-96"
                error={error}
            />

            <Button name="hentButton" variant="primary" size="medium" className="my-4" onClick={handleClick}>
                Hent
            </Button>
        </div>
    )
}

export default NLRequestForm
