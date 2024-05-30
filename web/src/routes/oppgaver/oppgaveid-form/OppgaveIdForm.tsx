import React, { ReactElement, useState } from 'react'
import { Button, TextField } from '@navikt/ds-react'

interface OppgaveIdFormProps {
    onChange: (oppgaveIder: number[]) => void
}

const OppgaveIdForm = ({ onChange }: OppgaveIdFormProps): ReactElement => {
    const [oppgaveIder, setOppgaveIder] = useState('')

    const handleClick: React.MouseEventHandler<HTMLButtonElement> = (event) => {
        event.preventDefault()
        onChange(
            oppgaveIder
                .split(',')
                .map(Number)
                .filter((numb) => numb != 0),
        )
    }

    return (
        <div>
            <TextField
                name="oppgaveider"
                label="oppgaveider"
                size="medium"
                onChange={(event) => {
                    setOppgaveIder(event.currentTarget.value)
                }}
                className="my-6 w-3/4"
            />
            <Button name="hentButton" variant="primary" size="medium" className="my-4" onClick={handleClick}>
                Hent
            </Button>
        </div>
    )
}

export default OppgaveIdForm
