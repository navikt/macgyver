import React, { ReactElement, useState } from 'react'
import { Button, TextField } from '@navikt/ds-react'

interface SykmeldingIdFormProps {
    onChange: (sykmeldingId: string) => void
}

const AltinnStatus  SykmeldingIdForm = ({ onChange }: SykmeldingIdFormProps): ReactElement => {
    const [sykmeldingId, setSykmeldingId] = useState('')

    const handleClick: React.MouseEventHandler<HTMLButtonElement> = (event) => {
        event.preventDefault()
        onChange(sykmeldingId)
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
            <Button name="hentButton" variant="primary" size="medium" className="my-4" onClick={handleClick}>
                Hent
            </Button>
        </div>
    )
}

export default AltinnStatusSykmeldingIdForm
