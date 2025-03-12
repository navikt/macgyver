import React, { ReactElement, useState } from 'react'
import { Button, TextField } from '@navikt/ds-react'

interface SykmeldingIdFormProps {
    onChange: (sykmeldingId: string, orgnummer: string) => void
}

const AltinnStatusSykmeldingIdForm = ({ onChange }: SykmeldingIdFormProps): ReactElement => {
    const [sykmeldingId, setSykmeldingId] = useState('')
    const [orgnummer, setOrgnummer] = useState('')

    const handleClick: React.MouseEventHandler<HTMLButtonElement> = (event) => {
        event.preventDefault()
        onChange(sykmeldingId, orgnummer)
    }

    return (
        <div>
            <TextField
                name="sykmeldingId"
                label="sykmeldingId"
                size="medium"
                onChange={(event) => {
                    setSykmeldingId(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                name="orgnummer"
                label="orgnummer"
                size="medium"
                required
                onChange={(event) => {
                    setOrgnummer(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <Button name="hentButton" variant="primary" size="medium" className="my-4" onClick={handleClick}>
                Hent
            </Button>
        </div>
    )
}

export default AltinnStatusSykmeldingIdForm
