import { placeOrder } from "api";
import { useState } from "react";
import { Alert, Button, Card, Container, FormControl, InputGroup, ToggleButton, ToggleButtonGroup } from "react-bootstrap";
import { useParams } from "react-router";
import { UserCredentials } from "types";

type ProductParam = {
    product: string
}

export const TradingScreen = ({ userCredentials }: { userCredentials: Partial<UserCredentials> }) => {

    const { product } = useParams<ProductParam>()

    const [side, setSide] = useState("");
    const [price, setPrice] = useState("");
    const [amount, setAmount] = useState("");

    return (
        <Container>
            <Alert className="mt-1" variant="success" data-test-id="welcome-user">You are now trading {product}</Alert>

            <Card>
                <Card.Body>
                    <ToggleButtonGroup data-test-id="side" className="mb-2" type="radio" name="radio">
                        <SideToggleButton side="bid" currentSideValue={side} setSide={setSide} />
                        <SideToggleButton side="ask" currentSideValue={side} setSide={setSide} />
                    </ToggleButtonGroup>
                    <br />
                    <InputGroup className="mb-3">
                        <FormControl
                            data-test-id="price"
                            placeholder="Price"
                            onChange={e => setPrice(e.target.value)}
                        />
                        <FormControl
                            data-test-id="amount"
                            placeholder="Amount"
                            onChange={e => setAmount(e.target.value)}
                        />
                    </InputGroup>
                    <Button
                        data-test-id="place"
                        variant="outline-primary"
                        onClick={() => placeOrder(userCredentials, product, side.toLocaleUpperCase(), price, amount)}>
                        Place Order
                    </Button>
                </Card.Body>
            </Card>
        </Container>
    )
}

type SideToggleButtonProps = {
    side: string
    currentSideValue: string
    setSide: (side: string) => void
}

const SideToggleButton = ({ side, currentSideValue, setSide }: SideToggleButtonProps) => (
    <ToggleButton
        key={side}
        id={`radio-${side}`}
        data-test-id={`radio-${side}`}
        type="radio"
        name="radio"
        variant="outline-success"
        value={side}
        checked={currentSideValue === side}
        onChange={(e) => setSide(e.currentTarget.value)}
    >
        {side.toLocaleUpperCase()}
    </ToggleButton>
)
