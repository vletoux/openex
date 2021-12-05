<?php

namespace App\Controller\Exercise\Tag;

use App\Controller\Base\BaseController;
use App\Entity\Tag;
use App\Form\Type\TagType;
use Doctrine\Persistence\ManagerRegistry;
use FOS\RestBundle\Controller\Annotations as Rest;
use JetBrains\PhpStorm\Pure;
use OpenApi\Annotations as OA;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;

class TagController extends BaseController
{
    private ManagerRegistry $doctrine;

    public function __construct(ManagerRegistry $doctrine, TokenStorageInterface $tokenStorage)
    {
        $this->doctrine = $doctrine;
        parent::__construct($tokenStorage);
    }
    
    /**
     * @OA\Response(
     *    response=200,description="Create a new tag")
     *
     * @Rest\View(statusCode=Response::HTTP_CREATED, serializerGroups={"tag"})
     * @Rest\Post("/api/tag")
     */
    public function postCreateTagAction(Request $request)
    {
        $em = $this->doctrine->getManager();

        $tag = new Tag();
        $form = $this->createForm(TagType::class, $tag);
        $form->submit($request->request->all());
        if ($form->isValid()) {
            $em->persist($tag);
            $em->flush();
            return $tag;
        } else {
            return $form;
        }
    }

    /**
     * @OA\Response(
     *    response=200,description="Edit a Tag")
     *
     * @Rest\View(statusCode=Response::HTTP_CREATED, serializerGroups={"tag"})
     * @Rest\Post("/api/tag/{tag_id}")
     */
    public function postEditTagAction(Request $request)
    {
    }

    /**
     * @OA\Response(
     *    response=200,
     *    description="Delete a tag"
     * )
     *
     * @Rest\View(statusCode=Response::HTTP_OK, serializerGroups={"tag"})
     * @Rest\Delete("/api/tag/{tag_id}")
     */
    public function deleteTagAction(Request $request)
    {
        $em = $this->doctrine->getManager();
        $tag = $em->getRepository('App:Tag')->find($request->get('tag_id'));

        if (empty($tag)) {
            return $this->tagNotFound();
        }
        $em->remove($tag);
        $em->flush();
        return array('result' => true);
    }

    private function tagNotFound()
    {
        return View::create(['message' => 'Tag not found'], Response::HTTP_NOT_FOUND);
    }

    /**
     * @OA\Response(
     *    response=200,
     *    description="Get List of Tags"
     * )
     *
     * @Rest\View(serializerGroups={"tag"})
     * @Rest\Get("/api/tag")
     */
    public function getTagAction(Request $request)
    {
        $em = $this->doctrine->getManager();
        $tags = $em->getRepository('App:Tag')->findAll();
        return $tags;
    }
}
