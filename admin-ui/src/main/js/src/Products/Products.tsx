import { Asset } from 'Assets/assets'
import { useState } from 'react'
import { Button, Card, Container, Form, Table } from 'react-bootstrap'
import { addProduct, getProducts } from './productRequests'
import { Product } from './products'

type ProductsProps = { assets: Asset[] }


export const Products = ({ assets }: ProductsProps) => {
    const [products, setProducts] = useState<Product[]>([])
    const [baseAssetId, setBaseAssetId] = useState<number>(-1);
    const [counterAssetId, setCounterAssetId] = useState<number>(-1);

    return (
        <Container className="p-3">
            <Card>
                <Card.Header>Products</Card.Header>
                <Card.Body>
                    <Table data-test-id="products-table" striped bordered hover>
                        <thead>
                            <tr>
                                <th>Product ID</th>
                                <th>Symbol</th>
                                <th>Base Exponent</th>
                                <th>Counter Exponent</th>
                                <th>Maker Fee (bps)</th>
                                <th>Taker Fee (bps)</th>
                            </tr>
                        </thead>
                        <tbody>
                            {products.map(product => (
                                <tr data-test-id={`product-row-${product.symbol}`}>
                                    <td>{product.productId}</td>
                                    <td>{product.symbol}</td>
                                    <td>{product.baseExponent}</td>
                                    <td>{product.counterExponent}</td>
                                    <td>{product.makerFee}</td>
                                    <td>{product.takerFee}</td>
                                </tr>
                            ))}
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>
            <br />
            <Card>
                <Card.Header>Add new product</Card.Header>
                <Card.Body>
                    <Form onSubmit={(event: any) => handleSubmit(baseAssetId, counterAssetId, event, setProducts)}>
                        <Form.Group controlId="formBaseAsset">
                            <Form.Label>Base Asset</Form.Label>
                            <Form.Control data-test-id="base-asset-select" as='select' onChange={e => setBaseAssetId(Number(e.target.value))}>
                                <option>Select Asset...</option>
                                {assets.map(asset => (
                                    <option value={asset.assetId}>{asset.symbol}</option>
                                ))}
                            </Form.Control>
                            <Form.Text className="text-muted">
                                Select base asset of the new product
                            </Form.Text>
                        </Form.Group>

                        <br />

                        <Form.Group controlId="formCounterAsset">
                            <Form.Label>Counter Asset</Form.Label>
                            <Form.Control data-test-id="counter-asset-select" as='select' onChange={e => setCounterAssetId(Number(e.target.value))}>
                                <option>Select Asset...</option>
                                {assets.map(asset => (
                                    <option value={asset.assetId}>{asset.symbol}</option>
                                ))}
                            </Form.Control>
                            <Form.Text className="text-muted">
                                Select counter asset of the new product
                            </Form.Text>
                        </Form.Group>

                        <br />

                        <Form.Group controlId="formMakerFee">
                            <Form.Label>Maker Fee</Form.Label>
                            <Form.Control data-test-id="maker-fee-input" name="makerFee" placeholder="Enter maker fee" />
                            <Form.Text className="text-muted">
                                Fee that maker will be charged for providing liquidity. Use negative numbers for rebates.
                            </Form.Text>
                        </Form.Group>

                        <br />

                        <Form.Group controlId="formtakerFee">
                            <Form.Label>Taker Fee</Form.Label>
                            <Form.Control data-test-id="taker-fee-input" name="takerFee" placeholder="Enter taker fee" />
                            <Form.Text className="text-muted">
                                Fee that taker will be charged for consuming liquidity. Use negative numbers for rebates.
                            </Form.Text>
                        </Form.Group>

                        <br />

                        <Button data-test-id="add-product-button" variant="primary" type="submit">
                            Add Product
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    )
}


const handleSubmit = (baseAssetId: number, counterAssetId: number, event: any, setProducts: (products: Product[]) => void) => {
    event.preventDefault()
    event.stopPropagation()

    const formData = new FormData(event.target)

    const makerFee = Number(formData.get("makerFee"))
    const takerFee = Number(formData.get("takerFee"))

    addProduct(baseAssetId, counterAssetId, makerFee, takerFee).then(() => {
        getProducts().then((response) => setProducts([...response.products]))
    })
}

