import { useEffect } from 'react'
import { Button, Card, Container, Form, Table } from 'react-bootstrap'
import { addAsset, getAssets } from './assetRequests'
import { Asset } from './assets'


type AssetsProps = { assets: Asset[], setAssets: (assets: Asset[]) => void }

export const Assets = ({ assets, setAssets }: AssetsProps) => {

    useEffect(() => {
        getAssets().then((response) => setAssets(response.assets))
    }, [])

    return (
        <Container className="p-3">
            <Card>
                <Card.Header>Assets</Card.Header>
                <Card.Body>
                    <Table data-test-id="assets-table" striped bordered hover>
                        <thead>
                            <tr>
                                <th>Asset ID</th>
                                <th>Symbol</th>
                                <th>Scale</th>
                            </tr>
                        </thead>
                        <tbody>
                            {assets.map(asset => (
                                <tr>
                                    <td>{asset.assetId}</td>
                                    <td>{asset.symbol}</td>
                                    <td>{asset.scale}</td>
                                </tr>
                            ))}
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>
            <br />
            <Card>
                <Card.Header>Add new asset</Card.Header>
                <Card.Body>
                    <Form onSubmit={(event: any) => handleSubmit(event, setAssets)}>
                        <Form.Group controlId="formSymbol">
                            <Form.Label>Symbol</Form.Label>
                            <Form.Control data-test-id="symbol-input" name="symbol" placeholder="Enter symbol" />
                            <Form.Text className="text-muted">
                                Symbol must be unique.
                            </Form.Text>
                        </Form.Group>
                        <Form.Group controlId="formScale">
                            <Form.Label>scale</Form.Label>
                            <Form.Control data-test-id="scale-input" name="scale" placeholder="Enter scale" />
                            <Form.Text className="text-muted">
                                Scale will determine the smallest denomination of an asset supported by the exchange.
                            </Form.Text>
                        </Form.Group>
                        <Button data-test-id="add-asset-button" variant="primary" type="submit">
                            Add Asset
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    )
}

const handleSubmit = (event: any, setAssets: (assets: Asset[]) => void) => {
    event.preventDefault()
    event.stopPropagation()

    const formData = new FormData(event.target)

    const symbol = formData.get("symbol") as string
    const scale = formData.get("scale") as string

    addAsset(symbol, scale).then(() => {
        getAssets().then((response) => setAssets([...response.assets]))
    })
}

