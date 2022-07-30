import { useEffect, useState } from "react"
import { Container, FormControl, InputGroup, Table } from "react-bootstrap"
import Button from "react-bootstrap/Button"
import Card from "react-bootstrap/Card"
import Form from "react-bootstrap/Form"
import { useHistory, useLocation } from "react-router-dom"
import { addUser, getUserBalances, getUsers, updateUserBalance } from "Users/userRequests"
import { User, UserBalances } from "Users/users"

export const Users = () => {
    const history = useHistory();
    const [users, setUsers] = useState<User[]>([])

    useEffect(() => {
        getUsers().then((response) => setUsers(response.users))
    }, [])

    return (
        <Container className="p-3">
            <Card>
                <Card.Header>Users</Card.Header>
                <Card.Body>
                    <Table data-test-id="users-table" striped bordered hover>
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>Username</th>
                                <th>Balances</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users!.map((user) => (<UserRow key={user.userId} user={user} history={history} />))}
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>
            <br />
            <Card>
                <Card.Header>Add new user</Card.Header>
                <Card.Body>
                    <Form onSubmit={(event: any) => handleSubmit(event, setUsers)}>
                        <Form.Group controlId="formUsername">
                            <Form.Label>Username</Form.Label>
                            <Form.Control data-test-id="username-input" name="username" placeholder="Enter username" />
                            <Form.Text className="text-muted">
                                Username must be unique.
                            </Form.Text>
                        </Form.Group>
                        <Form.Group controlId="formPassword">
                            <Form.Label>Password</Form.Label>
                            <Form.Control data-test-id="password-input" name="password" placeholder="Enter password" />
                            <Form.Text className="text-muted">
                                Password that will authenticate user.
                            </Form.Text>
                        </Form.Group>
                        <Button data-test-id="add-user-button" variant="primary" type="submit">
                            Add User
                        </Button>
                    </Form>
                </Card.Body>
            </Card>

        </Container>
    )
}

const handleSubmit = (event: any, setUsers: (users: User[]) => void) => {
    event.preventDefault()
    event.stopPropagation()

    const formData = new FormData(event.target)

    const username = formData.get("username") as string
    const password = formData.get("password") as string

    addUser(username, password).then(() => {
        getUsers().then((response) => setUsers([...response.users]))
    })
}


const UserRow = ({ user, history }: { user: User, history: any }) => {

    return (
        <tr>
            <td>{user.userId}</td>
            <td>{user.username}</td>
            <td>
                <Button data-test-id={`user-page-${user.username}`} variant="primary" onClick={() => {
                    history.push({
                        pathname: `/user/${user.userId}`,
                        state: { user: user }
                    })
                }}>
                    User Page
                </Button>
            </td>
        </tr>
    )
}


type LocationState = {
    user: User;
}

export const UserPage = () => {
    const [userBalances, setUserBalances] = useState<UserBalances>({})
    const location = useLocation<LocationState>();

    const user = location.state.user

    useEffect(() => {
        getUserBalances(user.userId).then((response) => setUserBalances(response.balances))
    }, [])

    return (
        <>
            <Container className="p-3">
                <Card>
                    <Card.Header>User Information</Card.Header>
                    <Card.Body>
                        User ID: {user.userId}
                        <br />
                        Username: {user.username}
                    </Card.Body>
                </Card>
                <br />
                <Card>
                    <Card.Header>User Balances</Card.Header>
                    <Card.Body>
                        <Table data-test-id="users-table" striped bordered hover>
                            <thead>
                                <tr>
                                    <th>Asset</th>
                                    <th>Existing Balance</th>
                                    <th>Balance Adjustment Amount (+/-)</th>
                                    <th>Update</th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.entries(userBalances)
                                    .map(([asset, existingBalance]) => (<UserBalanceRow userId={user.userId} asset={asset} existingBalance={existingBalance} setUserBalances={setUserBalances} />))}
                            </tbody>
                        </Table>
                    </Card.Body>
                </Card>
            </Container>
        </>
    )
}

const UserBalanceRow = ({ userId, asset, existingBalance, setUserBalances }: { userId: number, asset: string, existingBalance: number, setUserBalances: (b: UserBalances) => void }) => {
    const [balanceAdjustment, setBalanceAdjustment] = useState("")

    return (
        <tr>
            <td>{asset}</td>
            <td data-test-id={`balance-${asset}`}>{existingBalance}</td>
            <td>
                <InputGroup>
                    <FormControl data-test-id={`update-balance-input-${asset}`} placeholder="Enter a number..." onChange={e => setBalanceAdjustment(e.target.value)} />
                </InputGroup>
            </td>
            <td>
                <Button data-test-id={`update-balance-button-${asset}`} variant="primary" type="submit" onClick={() => updateUserBalance(userId, asset, Number(balanceAdjustment)).then(() => {
                    getUserBalances(userId).then((response) => setUserBalances(response.balances))
                })}>
                    Update Balance
                </Button>
            </td>
        </tr >
    )
}