import React, { ReactElement } from 'react'
import { Button, TextField } from '@navikt/ds-react'

interface FnrFormProps {
    onChange: (fnr: string) => void
    refetch: () => void
}

const FnrForm = ({ onChange, refetch }: FnrFormProps): ReactElement => {
    const handleClick: React.MouseEventHandler<HTMLButtonElement> = () => {
        refetch()
    }

    return (
        <div>
            <TextField
                name="fnr"
                label="fnr/dnr/aktorid"
                size="medium"
                onChange={(event) => {
                    onChange(event.currentTarget.value)
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
