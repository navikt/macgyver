import { ReactElement } from 'react'
import { BodyLong, Button, Modal } from '@navikt/ds-react'

interface ConfirmationModalProps {
    open: boolean
    message: string
    onCancel: () => void
    onOK: () => void
}

const ConfirmModal = ({ open, message, onCancel, onOK }: ConfirmationModalProps): ReactElement => {
    return (
        <Modal open={open} aria-label="Modal demo" onClose={onCancel}>
            <Modal.Body>
                <BodyLong spacing>{message}</BodyLong>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={onCancel}>
                    Nei
                </Button>
                <Button variant="danger" onClick={onOK}>
                    Ja eg er sikker
                </Button>
            </Modal.Footer>
        </Modal>
    )
}

export default ConfirmModal
