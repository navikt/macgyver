import React, { ReactElement, useState } from 'react'
import { Button, TextField } from '@navikt/ds-react'

interface FnrFormProps {
    onChange: (fnr: string) => void
}

const FnrForm = ({ onChange }: FnrFormProps): ReactElement => {
    const [fnr, setFnr] = useState('')

    const handleClick: React.MouseEventHandler<HTMLButtonElement> = (event) => {
        event.preventDefault()
        onChange(fnr)
    }

    return (
        <div>
            <TextField
                name="fnr"
                label="fnr/dnr/aktorid"
                size="medium"
                onChange={(event) => {
                    setFnr(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <Button name="hentButton" variant="primary" size="medium" className="my-4" onClick={handleClick}>
                Hent
            </Button>
        </div>
    )
}

export default FnrForm
